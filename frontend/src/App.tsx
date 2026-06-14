import { Box, Container, Paper, Typography } from '@mui/material'

function App() {
  return (
    <Box component="main" sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 3 }}>
      <Container maxWidth="sm">
        <Paper elevation={3} sx={{ p: { xs: 4, sm: 6 }, textAlign: 'center' }}>
          <Typography component="h1" variant="h3" sx={{ fontWeight: 700 }}>
            Real Estate SaaS MVP
          </Typography>
        </Paper>
      </Container>
    </Box>
  )
}

export default App
